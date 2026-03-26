document.addEventListener("DOMContentLoaded", () => {
	const quantityInputs = document.querySelectorAll(".quantity");
	quantityInputs.forEach((input) => {
		input.addEventListener("change", () => {
			const id = input.dataset.id;
			const quantity = parseInt(input.value, 10);
			if (!id) return;
			if (!Number.isInteger(quantity) || quantity < 1) {
				input.value = "1";
				return;
			}
			window.location.href = `/cart/updateCart/${id}/${quantity}`;
		});
	});
});
